# JimdoStats

JimdoStats is a small demo app that combines:
* a backend to provide mock statistics data as a RESTfull service
* a client that consumes this service and presents data in graph/chart form

App utilizes following open source libraries: 

- [WilliamChart](https://github.com/diogobernardino/WilliamChart)
- [EazeGraph](https://github.com/blackfizz/EazeGraph)
- [Nanohttpd](http://nanohttpd.org/)
- [Volley](https://developer.android.com/training/volley/index.html)

Changelog
---------

- 25.06.2015
 * Prevent main activity from getting destroyed when back button is pressed 
   to avoid extra server request calls.
 
- 25.06.2015
 * Added backend with NanoHTTP Server to provide mock data as a RESTfull webservice
 * Consume service with google volley
 * JSON marshaling and unmarshaling
 * Save/Restore data in onSaveInstanceState/onRestoreInstanceState
 * Extracted DataManager Class to handle data transformations
 * 2-way fling listner
 * bugfixes in line chart presentation
 
- 24.06.2015
 * Popup window with devices statistics and an animated PieChart
 * Code/Package restructure
 * Minor bugfixes and UI improvements
 * Improve names and comments

License
-------

This software is released under the MIT License, see [LICENSE](https://github.com/ar90n/serverless-s3-local/blob/master/LICENSE).
