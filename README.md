# zot-gallery-experimental
A simple Android app that detects nearby Estimote Beacons and displays different information accordingly for each Beacon.

Simulates an Art Gallery. Each beacon is associated with an art piece.
• Detects all beacons associated with one UUID.

• Chooses the nearest beacon as the art "piece", and displays the exhibit its in, the name of the piece, and information about the piece. -> Major ID gives the Exhibit
-> Minor ID gives the Piece
    
• Currently, 3 beacons have their major and minor IDs hardcoded as 3 distinct art pieces.
-> 2 exhibits - "Blueberry Exhibit" and "Ice-Mint Exhibit"
-> All other beacons are detected, but have no data on them. They are reported as "Unknown" pieces and exhibits, and their major and   minor ID are displayed instead.
