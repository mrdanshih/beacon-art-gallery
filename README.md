# zot-gallery-experimental
A simple Android app that detects nearby Estimote Beacons and displays different information accordingly for each Beacon.

Simulates an Art Gallery. Each beacon is associated with an art piece. Art piece & beacon association is stored on a database.
• Detects all beacons associated with one UUID.

• Chooses the nearest beacon as the art "piece", the name of the piece, and information about the piece. -> Major ID is associated with art piece ID.
    
• On start, app connects to the UCI Art Gallery database and stores information for all the part pieces locally.

• For the nearest beacon detected, app attempts to match the detected beacon to its associated artpiece from the database. If one is found, information for that piece is displayed on screen. Else, if the detected beacon does not have an associated art piece in the database, the major ID is displayed only.
