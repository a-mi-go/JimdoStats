This file give a little overview of code structure and activities.

Here is a rough classdiagram:
![Classdiagram](https://github.com/Muxagen/JimdoStats/blob/master/doc/Classdiagram.png)

Basically there are 3 main packages:

[backend](https://github.com/Muxagen/JimdoStats/tree/master/app/src/main/java/de/goldenzweig/jimdostats/backend) - simulates the backend within the app. JimdoStatsHTTPD extends the NanoHTTPD library class and provides a basic RESTfull webservice.
On creation, JimdoStatsHTTPD fetches mock data from JimdoStatisticsMockDataProvider and serves this data in JSON format.

[model](https://github.com/Muxagen/JimdoStats/tree/master/app/src/main/java/de/goldenzweig/jimdostats/model) - agregates all model classes used both by the backend and the app itself. Data transfered from the server to the app is a list of JimdoOneDayStatistics objects.
Each JimdoOneDayStatistics object contains a date and list of Visits that happened on that date. Each Visit has a referer, device and os of the visitor and a list of PageView objects. Each PageView contains information about the viewed page and time spent on that page.

[app](https://github.com/Muxagen/JimdoStats/tree/master/app/src/main/java/de/goldenzweig/jimdostats/app) - this is the main part of the app with all activities, requests, ui, etc.. AppController handles the volley request queue and the creation/start of the server. This ist the entry point of the whole app. Main activity handles all of the UI, activity lifecycle and request/response code. All data processing, transformation and preperation is done in the DataManager class. Class Constants contatins constants that are used by both MainActivity and DataManager. Presentation classes are backed in the DataManager and utilized in the MainActivity to draw charts and diagrams.
