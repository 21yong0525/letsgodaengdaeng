package kr.or.mrhi.letsgodaengdaeng.view.fragment.community

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kr.or.mrhi.letsgodaengdaeng.dataClass.CommunityVO
import kr.or.mrhi.letsgodaengdaeng.databinding.FragmentAllBinding
import kr.or.mrhi.letsgodaengdaeng.firebase.CommunityDAO
import kr.or.mrhi.letsgodaengdaeng.view.activity.MainActivity
import kr.or.mrhi.letsgodaengdaeng.view.adapter.CustomAdapter
import kr.or.mrhi.letsgodaengdaeng.view.fragment.CommunityFragment


class AllFragment : Fragment() {

    lateinit var binding : FragmentAllBinding
    lateinit var communityList: MutableList<CommunityVO>
    lateinit var adapter: CustomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /** 화면이 다시 넘어올 때 어댑터를 다시 연결 */
    override fun onResume() {
        super.onResume()
        selectUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAllBinding.inflate(inflater, container, false)
        communityList = mutableListOf()
        adapter = CustomAdapter(requireContext(),communityList)
        val linearLayout = LinearLayoutManager(context)
        linearLayout.reverseLayout = true
        linearLayout.stackFromEnd = true
        binding.recyclerviewAll.layoutManager = linearLayout
        binding.recyclerviewAll.adapter = adapter

        selectUser()

        return binding.root
    }

    /** 모든 게시물을 불러온다 단 type에 따라서 동네범위 설정 */
    fun selectUser() {
        val communityDAO = CommunityDAO()
        val communityFragment = (context as MainActivity).communityFragment
        communityDAO.selectCommunity()?.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                communityList.clear()
                for (userdata in snapshot.children) {
                    //json 방식으로 넘어오기 때문에 클래스 방식으로 변환해야함
                    val community = userdata.getValue(CommunityVO::class.java)
                    //비어있던 userKey 부분에 key 값을 넣어준다
                    community?.docID = userdata.key.toString()
                    if (community != null) {
                        if (communityFragment.type == 0) {
                            communityList.add(community)
                        } else {
                            if (community.local.equals(MainActivity.userInfo.address)) {
                                communityList.add(community)
                            }
                        }
                    }
                }// end of for
                adapter.notifyDataSetChanged()
            }// end of onDataChange

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "가져오기 실패 $error", Toast.LENGTH_SHORT).show()
                Log.e("firebasecrud22", "selectUser() ValueEventListener cancel $error")
            }
        })
    }
}
