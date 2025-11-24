package com.example.appblocker.ui.blocked

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appblocker.databinding.ActivityBlockedAppsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BlockedAppsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockedAppsBinding
    private val vm: BlockedAppsViewModel by viewModels()
    private lateinit var adapter: InstalledAppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = InstalledAppAdapter(
            onToggle = { app, checked ->
                vm.toggle(app, checked)
            }
        )

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.searchInput.doOnTextChanged { text, _, _, _ ->
            vm.updateQuery(text?.toString() ?: "")
        }

        binding.btnSelectAll.setOnClickListener {
            val all = adapter.currentList.map { it.packageName }.toSet()
            val now = vm.getBlocked()
            val toAdd = all - now
            if (toAdd.isNotEmpty()) {
                toAdd.forEach { pkg -> vm.toggle(adapter.find(pkg)!!, true) }
            }
        }

        binding.btnClearAll.setOnClickListener {
            adapter.currentList.forEach { app ->
                if (vm.isBlocked(app.packageName)) vm.toggle(app, false)
            }
        }

        lifecycleScope.launch {
            vm.apps.collectLatest { list ->
                adapter.submitList(list)
                binding.emptyView.root.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}