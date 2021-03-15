package com.aglushkov.coroutinesrxjavabenchmark.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aglushkov.coroutinesrxjavabenchmark.R
import com.aglushkov.coroutinesrxjavabenchmark.benchmark.CoroutineBenchmark
import com.aglushkov.coroutinesrxjavabenchmark.benchmark.IOSources
import com.aglushkov.coroutinesrxjavabenchmark.benchmark.RxJavaBenchmark
import com.aglushkov.coroutinesrxjavabenchmark.benchmark.SystemMonitor
import com.google.android.material.slider.Slider

class HomeFragment : Fragment() {

    private lateinit var monitor: SystemMonitor
    private lateinit var coroutineBenchmark: CoroutineBenchmark
    private lateinit var rxBenchmark: RxJavaBenchmark
    private lateinit var homeViewModel: HomeViewModel

    private var useCpuDispatcher: Boolean = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        val button = root.findViewById<Button>(R.id.button)
        button.setOnClickListener {
            textView.text = "coroutineBenchmark started"
            coroutineBenchmark.runBenchmark {
                showMonitorResult("Coroutines isSuspended:${coroutineBenchmark.useSuspended} useDefaultDispatcher:${coroutineBenchmark.useDefaultDispatcher}", textView)
            }
        }

        val isSuspendedSwitch = root.findViewById<Switch>(R.id.is_suspended_switch)
        isSuspendedSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            coroutineBenchmark = CoroutineBenchmark(monitor, isChecked, useCpuDispatcher)
        }

        val button2 = root.findViewById<Button>(R.id.button2)
        button2.setOnClickListener {
            textView.text = "rxBenchmark started"
            rxBenchmark.runBenchmark {
                showMonitorResult("RxJava useDefaultDispatcher:${rxBenchmark.useComputationScheduler}", textView)
            }
        }

        val cpuDispatcherSwitch = root.findViewById<Switch>(R.id.use_cpu_dispatcher)
        cpuDispatcherSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            useCpuDispatcher = isChecked
            coroutineBenchmark = CoroutineBenchmark(monitor, coroutineBenchmark.useSuspended, useCpuDispatcher)
            rxBenchmark = RxJavaBenchmark(monitor, useCpuDispatcher)
        }

        val delayStepEditText = root.findViewById<EditText>(R.id.delay_step_edit)
        delayStepEditText.setText(IOSources.delayStep.toString())

        delayStepEditText.addTextChangedListener(afterTextChanged = {
            it?.let {
                IOSources.updateDelayStep(it.toString().toLongOrNull() ?: 0)
            }
        })

        return root
    }

    private fun showMonitorResult(tag: String, textView: TextView) {
        val availableMemInMB = monitor.minAvailableMemory / 1024 / 1024
        val availableMemOnStartInMB = monitor.minAvailableMemoryOnStart / 1024 / 1024
        textView.text = "" + tag + "\ntime: " + monitor.time +
                "\nmaxThreadCount: " + monitor.maxThreadCount +
                "\nminAvailableMemory: ${availableMemInMB} MB" +
                "\nAllocated: ${availableMemOnStartInMB - availableMemInMB} MB"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        monitor = SystemMonitor(requireContext())
        monitor.updateAvailableMemoryOnStart {
            coroutineBenchmark = CoroutineBenchmark(monitor, false, useCpuDispatcher)
            rxBenchmark = RxJavaBenchmark(monitor, false)
        }
    }
}
