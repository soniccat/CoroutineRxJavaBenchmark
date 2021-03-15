Benchmarks to see the difference in resource consumption between Coroutines and RxJava. 

# To set up benchmarks
In IOSources add web sites you want to load during benchmarks. I prefer to add about 80-100 items:  
`<Add here websites you want to load>`

# What benchmark does
Now there is only one benchmark and it's related only with loading website main pages from the internet using OkHttp. The intention is to simulate interaction with api in real apps. There're several parameters:
* IOSources.delayStep specifies how many millisecond we need to wait before starting with loading the next page. With a small value like 10ms we generate much more work to do simultaneously as new connections launch before the previous ones finish.
* In the constructors you can choose to run this benchmark on a Computation scheduler
* For Coroutines you can enable or disable using suspended functions

The related classes: **RxJavaBenchmark** and **CoroutineBenchmark**

# What benchmark measures
During a benchmark we monitor:  
* Maximum allocated memory using ActivityManager.MemoryInfo / ActivityManager.getMemoryInfo
* Maximum thead count using Thread.activeCount()
* How much time it takes to finish

The related classes: **SystemMonitor**

# The current results

With 250ms delay everything works similarly
![250_delay](https://user-images.githubusercontent.com/147129/111115403-9d338800-8575-11eb-853c-d8c6894af1c7.jpg)

With 10ms delay I see more threads allocated with time profit. Also it seems using suspended functions only consume more memory without any profit.
![10_delay](https://user-images.githubusercontent.com/147129/111115530-c9e79f80-8575-11eb-9d27-b02624674961.jpg)

With 10ms on a Computation scheduler I see many thread with suspended functions. As we suspend work on Default threads after starting a connection we reuse this thread and start another connection using OkHttp. Then OkHttp creates its own thread every time and generates as much work as in the previous results.  
![10_delay_on_computation](https://user-images.githubusercontent.com/147129/111115723-1337ef00-8576-11eb-81b1-222587d82cfe.jpg)
