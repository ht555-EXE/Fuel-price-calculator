# Introduction

For this android development coursework, I chose to develop a Fuel Price Calculator app. The intention of the app is to be integrated with existing services to get data regarding a vehicle and a trip that vehicle undertakes without requiring much manual data entry. This is alongside calculating average fuel prices across the country. The intention is that with a vehicles efficiency and distance covered, a predicted fuel cost can be calculated beforehand. Key features include:

- The ability to add multiple vehicles, petrol or diesel, requiring only the vehicles license plate
- The ability to create multiple trips per vehicle, with autocomplete for origin and destination addresses
- Meaningful statistics regarding trips being displayed such as start/end locations, distance in either miles or km, and cost in GBP
- Settings menu allowing users to changed preferred measurement system and view current fuel prices, including when these were last updated
- Deletable cars and trips through swiping each row right to left

For the time being API secrets have been left hard coded, with the intention to remove these in a deployable build. Given this the app should work out of the box with android studio AVDs.

# Design Rationale

The application utilises Jetpack Compose with composables in favour of traditional XML-based views. Lazy Components such as LazyColumn are used in place of RecyclerView, reducing boilerplate in external XML files, improving readability and maintainability. Using Jetpack allowed me to better leverage and learn the modern kotlin tech stack, alongside dynamically changing the ui with added trip and car instances through composes smart recomposition.

The application is structured across several activities each constituting a different screen (Home, settings, change car, add trip), swapping between these using intents. This ensures clear modularity between each screen.

For persistent storage, my application uses the Room persistence library, allowing for scalability through a familiar SQL backend. Room also allowed for relational implementations, in this instance the one-to-many relation between cars and trips. This is alongside Preferences DataStore utilised for configuration persistence in settings and current fuel price values.

Kotlin Coroutines are employed for background processing including daily price synchronisation alongside background execution of database queries for changes in trips and cars.

For external services:
- For car services: I utilised DVLAs free vehicle enquiry API service for building car information from a license plate, such as fuel type and C02 emissions both used in calculating the efficiency of a given vehicle. This was alongside Motor Fuel Group's (MFG) petrol station dataset which I calculate a national average from, this dataset is updated regularly allowing for up to date information.
- For trip navigation services: I utilised Google Places, as an integrated feature of android, to employ autocomplete location features alongside Google Maps Distance Matrix for getting the distance and duration of a trip between two location ids gained from Google Places.

# Novel Features

- The app dynamically calculates estimated trip fuel costs based on the selected car’s fuel efficiency and synchronised fuel prices. This reduces manual calculation effort and provides users with real-time financial insight before completing a journey.
- The app is able to get information on most vehicles through only a license plate, using the API service for the rest of the information.
- Multiple vehicles can be set up, with only trips for that specific vehicle showing in the menu, persisting beyond vehicle changes.

# Challenges

- Given only free APIs have been used for the context of this project I was restricted in information I could gather, for instance vehicles currently do not store a model, leading vehicles to only be identified by make and colour
- The Fuel price checker currently only uses one petrol companies dataset, not reflecting an average of all UK petrol stations
- API secrets were difficult to configure locally, with maven often producing errors I could not work out how to fix
- The application currently only supports petrol and diesel vehicles, with no free vehicle retrieval APIs being fit for electric or hybrid statistics

# Future Improvements

- Adding support across europe, this would involve a significantly larger fuel station dataset, alongside handling different currencies
- Adding Electric and Hybrid vehicle support