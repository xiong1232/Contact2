package my.edu.tarc.contact

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import my.edu.tarc.contact.databinding.FragmentFirstBinding
import my.edu.tarc.mycontact.WebDB
import my.tarc.mycontact.Contact
import my.tarc.mycontact.ContactAdapter
import my.tarc.mycontact.ContactViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.net.UnknownHostException

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), MenuProvider {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //Refer to the ViewModel created by the Main Activity
    private val myContactViewModel: ContactViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        //Let ProfileFragment to manage the Menu
        val menuHost: MenuHost = this.requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner,
            Lifecycle.State.RESUMED)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ContactAdapter()

        //Add an observer
        myContactViewModel.contactList.observe(
            viewLifecycleOwner,
            Observer {
                if(it.isEmpty()){
                    binding.textViewCount.text =
                        getString(R.string.no_record)
                }else{
                    binding.textViewCount.isVisible = false
                    adapter.setContact(it)
                }
            }
        )
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        //do nothing here
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if(menuItem.itemId == R.id.action_download){
            //TODO: Download all user records from the web server
            downloadContact(requireContext(),getString(R.string.server_url)+
                    getString(R.string.read_user_url))
        }
        return true
    }

    fun downloadContact(context: Context, url: String){
        binding.progressBar.isVisible = true
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                // Process the JSON
                try {
                    if (response != null) {
                        val strResponse = response.toString()
                        val jsonResponse = JSONObject(strResponse)
                        val jsonArray: JSONArray = jsonResponse.getJSONArray("records")
                        val size: Int = jsonArray.length()

                        if(myContactViewModel.contactList.value?.isNotEmpty()!!){
                            myContactViewModel.deleteAll()
                        }

                        for (i in 0..size - 1) {
                            var jsonContact: JSONObject = jsonArray.getJSONObject(i)
                            var contact = Contact(
                                jsonContact.getString("name"),
                                jsonContact.getString("contact")
                            )
                            myContactViewModel.addContact(Contact(contact?.name!!, contact?.phone!!))
                        }
                        Toast.makeText(context, "$size record(s) downloaded", Toast.LENGTH_SHORT).show()
                        binding.progressBar.isVisible = false
                    }
                }catch (e: UnknownHostException){
                    Log.d("ContactRepository", "Unknown Host: %s".format(e.message.toString()))
                    binding.progressBar.isVisible = false
                }
                catch (e: Exception) {
                    Log.d("ContactRepository", "Response: %s".format(e.message.toString()))
                    binding.progressBar.isVisible = false
                }
            },
            { error ->
                Log.d("ContactRepository", "Error Response: %s".format(error.message.toString()))
            },
        )

        //Volley request policy, only one time request
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            0, //no retry
            1f
        )

        // Access the RequestQueue through your singleton class.
        WebDB.getInstance(context).addToRequestQueue(jsonObjectRequest)
    }//end of function
}//end of class
