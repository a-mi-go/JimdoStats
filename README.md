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
 * Popup window with devices statistics and an animated PieChart!
 * Code/Package restructure
 * Minor bugfixes and UI improvements
 * Improve names and comments

License
-------

The MIT License

Copyright (c) 2015 Mikhail Goldenzweig

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
