package kr.or.mrhi.letsgodaengdaeng.view.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kr.or.mrhi.letsgodaengdaeng.R
import kr.or.mrhi.letsgodaengdaeng.dataClass.User
import kr.or.mrhi.letsgodaengdaeng.databinding.ActivitySignupUserBinding
import kr.or.mrhi.letsgodaengdaeng.firebase.UserDAO
import java.util.concurrent.TimeUnit

class SignupUserActivity : AppCompatActivity() {
    companion object {
        var address: String? = null
    }
    lateinit var binding: ActivitySignupUserBinding

    val TAG = this.javaClass.simpleName
    val auth = Firebase.auth
    var verificationId: String? = null
    var passwordFlag = false
    var passwordCheckFlag = false
    var nicknameFlag = false
    var user: User? = null
    var userImageUri = Uri.parse("android.resource://kr.or.mrhi.letsgodaengdaeng/${R.drawable.default_person}")
    var requestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        Listener()

        requestLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                Glide.with(applicationContext)
                    .load(it.data?.data)
                    .into(binding.ivUserImage)
            }
            userImageUri = it.data?.data
        }
    }

    /** ?????? ????????? ????????? ????????? ??????????????? ????????? */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this@SignupUserActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /** ????????? ?????? ?????? */
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@SignupUserActivity, "????????? ????????? ?????????????????????.", Toast.LENGTH_SHORT)
                        .show()
                    Log.d(TAG, "????????? ?????? ??????")//????????????
                    binding.btnNext.isEnabled = true
                    binding.btnPhone.isEnabled = false
                    binding.btnPhoneCheck.isEnabled = false
                    binding.edtPhone1.isEnabled = false
                    binding.edtPhone2.isEnabled = false
                    binding.edtPhone3.isEnabled = false
                    binding.edtPhoneCheck.isEnabled = false
                } else {
                    Toast.makeText(this, "?????? ????????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "????????? ?????? ??????")//????????????
                }
            }
    }

    /** ????????? ?????? */
    fun Listener() {
        auth.signOut()
        /** ?????? ????????? ????????? ?????? */
        binding.ivUserImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            requestLauncher.launch(intent)
        }

        /** ????????? ?????? ????????? ????????? ???????????? ????????????????????? ?????? */
        binding.ivUserImage.setOnLongClickListener {
            Glide.with(this@SignupUserActivity)
                .load(R.drawable.default_person)
                .into(binding.ivUserImage)
            userImageUri = Uri.parse("android.resource://kr.or.mrhi.letsgodaengdaeng/${R.drawable.default_person}")
            return@setOnLongClickListener true
        }

        /** ????????? ???????????? : ?????? ????????? ?????????????????? ????????? ?????? && ???,??? ?????? ?????? 8?????? ???????????? ?????? ????????? , ????????? ??????  */
        binding.edtPhone2.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (text?.length == 4) {
                    binding.edtPhone3.requestFocus()
                }
                if ((text!!.length + binding.edtPhone3.text.length) == 8) {
                    binding.btnPhone.isEnabled = true
                    softkeyboardHide(binding.edtPhone2)
                } else {
                    binding.btnPhone.isEnabled = false
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })

        /** ????????? ???????????? : ???,??? ?????? ?????? 8?????? ???????????? ?????? ????????? , ????????? ?????? */
        binding.edtPhone3.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if ((text!!.length + binding.edtPhone2.text.length) == 8) {
                    binding.btnPhone.isEnabled = true
                    softkeyboardHide(binding.edtPhone3)
                } else {
                    binding.btnPhone.isEnabled = false
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })

        /** ???????????? ?????? ????????? ????????? ??????????????? ???????????? ?????? */
        binding.btnPhone.setOnClickListener {
            val userDAO = UserDAO()
            var phone = "010${binding.edtPhone2.text}${binding.edtPhone3.text}"
            /** ???????????? ????????? ?????? ????????? ????????? Single ?????? */
            userDAO.selectUserType("phone", phone)
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) =
                        if (snapshot.value != null) {
                            binding.btnPhoneCheck.isEnabled = false
                            binding.edtPhoneCheck.text.clear()
                            binding.edtPhone2.text.clear()
                            binding.edtPhone3.text.clear()
                            softkeyboardHide(binding.edtPhone3)
                            Toast.makeText(this@SignupUserActivity, "?????? ????????? ???????????? ?????????.", Toast.LENGTH_SHORT).show()
                        } else {
                            val callbacks =
                                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                                    override fun onVerificationFailed(e: FirebaseException) {
                                    }
                                    override fun onCodeSent(
                                        verificationId: String,
                                        token: PhoneAuthProvider.ForceResendingToken
                                    ) {
                                        this@SignupUserActivity.verificationId = verificationId
                                    }
                                }
                            phone = "+8210${binding.edtPhone2.text}${binding.edtPhone3.text}"
                            val options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber("$phone")
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(this@SignupUserActivity)
                                .setCallbacks(callbacks)
                                .build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                            binding.btnPhoneCheck.isEnabled = true
                        }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "btnPhone $error")
                    }
                })
        }

        /** ????????? ???????????? ?????? ????????? ????????? ???????????? ?????? ??? ????????? ????????? */
        binding.btnPhoneCheck.setOnClickListener {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, "${binding.edtPhoneCheck.text}")
            signInWithPhoneAuthCredential(credential)
        }

        /** ???????????? ?????? ?????? */
        binding.edtPassword.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (text!!.matches("^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#$%^&*])(?=.*[0-9!@#$%^&*]).{4,10}$".toRegex())) {
                    binding.tilPassword.isErrorEnabled = false
                    passwordFlag = true
                } else {
                    binding.tilPassword.error = "??????,??????,???????????? ????????? ?????? , 4??? ?????? ??????????????????"
                    passwordFlag = false
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })

        /** ???????????? ?????? : password ??? passwordCheck ??? ????????? ?????????????????? ????????? */
        binding.edtPasswordCheck.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (text.toString() == binding.edtPassword.text.toString()) {
                    binding.tilPasswordCheck.isErrorEnabled = false
                    passwordCheckFlag = true
                } else {
                    binding.tilPasswordCheck.error = "??????????????? ??????????????????"
                    passwordCheckFlag = false
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(text: Editable?) {}
        })


        /** ?????? ???????????? ?????? ??? ?????? ?????? */
        binding.edtAddress.setOnClickListener {
            val intent = Intent(this@SignupUserActivity, AddressActivity::class.java)
            startActivity(intent)
        }

        /** ????????? ?????? ?????? */
        binding.edtNickname.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (text!!.matches("^[???-???a-zA-Z0-9]{2,10}$".toRegex())) {
                    binding.tilNickname.isErrorEnabled = false
                    binding.btnNicknameCheck.isEnabled = true
                } else {
                    binding.tilNickname.error = "????????? ???????????? 2??? ?????? ??????????????????"
                    binding.btnNicknameCheck.isEnabled = false
                }
                nicknameFlag = false
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })

        /** ????????? ?????? ?????? */
        binding.btnNicknameCheck.setOnClickListener {
            val userDAO = UserDAO()
            userDAO.selectUserType("nickname", binding.edtNickname.text.toString())
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value != null) {
                            nicknameFlag = false
                            binding.edtNickname.requestFocus()
                            binding.tilNickname.error = "?????? ?????? ??????????????????."
                        } else {
                            nicknameFlag = true
                            Toast.makeText(
                                this@SignupUserActivity,
                                "??????????????? ??????????????????.",
                                Toast.LENGTH_SHORT
                            ).show()
                            softkeyboardHide(binding.edtNickname)
                            binding.btnNicknameCheck.isEnabled = false
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "btnNicknameCheck $error")
                    }
                })
        }

        /** ?????? ?????? ????????? ????????? ?????? ?????? ??????????????? ?????? */
        binding.btnNext.setOnClickListener {
            if (nicknameFlag && passwordFlag && passwordCheckFlag && binding.edtAddress.text.isNotEmpty()) {
                softkeyboardHide(binding.edtIntroduce)

                val userCode = auth.uid
                val phone = "${binding.edtPhone1.text}${binding.edtPhone2.text}${binding.edtPhone3.text}"
                val password = binding.edtPassword.text.toString()
                val nickname = binding.edtNickname.text.toString()
                val introduce = binding.edtIntroduce.text.toString()

                user = User(phone, password, nickname, introduce, address, "0")

                val intent = Intent(this@SignupUserActivity, SignupPuppyActivity::class.java)
                intent.putExtra("userCode", userCode)
                intent.putExtra("user", user)
                if (userImageUri != null) {
                    intent.putExtra("userImageUri", userImageUri)
                }

                startActivity(intent)
                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit)
                finish()
            } else {
                Toast.makeText(this@SignupUserActivity, "?????? ????????? ??????????????????", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** ????????? ?????? */
    fun softkeyboardHide(editText: EditText) {
        editText.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    /** ?????? ?????? ??? ?????? ?????? null ??? ???????????? ?????? ???????????? ?????? ???????????? */
    override fun onRestart() {
        super.onRestart()
        if (address != null) {
            binding.edtAddress.setText(address)
        }
    }

    /** ????????? ????????? ?????? ????????????????????? ????????? ????????????(Authentication ???????????? uid)
    ?????????????????? ????????? ???????????? ?????? ?????? ?????????????????? ??? ????????? ???????????? ???????????? SignupPuppy ?????? ?????? ???????????? */
    override fun onDestroy() {
        super.onDestroy()
        if (user == null) {
            auth.currentUser?.delete()
        }
    }

}
