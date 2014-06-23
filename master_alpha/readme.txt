ad hoc realtime generalization with javascript and java.




check list:

read file - check

convert to polygon - check

write json file - check

visualization of self created json - check

r- tree (save polygons per file/per extent)- check

selection based on extent/zoom - check 

selection operator/delete smallest polygons based on extent - check

typification based on area (nearest neighbors of biggest area - get extent, buffer biggest to extent, delete remaining polygons) - more or less check
->Problem - its more like an absorption - sort polygons(area) - get neighbors via extent expansion- delete them, increase polygon - comment

new nearest neighbour - fixed jts classes for proper nN method + remove methode + new replace method for clustering - check

complete for nN method with pre selection + aggregation for intersections. - check

nN method quite slow (90% of request): requests between 10ms - 3000ms; - comment
multithreads for nN? - idea? would be cool (!)

implemented nN Typification based on nN and replacing based on area - check  (!)

replacing only based on centroid (save previous centroids of clustered objects) (does it make sense?) - open

typification based on smallest area + nN - open 

typification based on smallest area, nN, orientation (smallest bounding box) - open

is there a better rtree version (not jts) ? - open

quad tree - open

kd tree (supposed to be faster than rtree - with points only) - open 

simplification douglas-peucker/visvalingam - open

overlaping/intersection aggregation - check

aggregation/amalgamation, polygons with same outline - open

aggregation, polygons based on distance - open

based on tgap - save polygon for different zoomlevels, not only highest level - (could be useful -> nN is slow) - open

speed test ( only system miili secs for now) / Junit would be nice- half checked

test for same small data size which will be send - limit via toepfers radical law + max filesize calculation - check

create different client sets ( thin for speed - op changer - view comparison orig-tree ) - open

big data tests - open

topology checks - open

different datasets - prepared but open

modular method combination (client) - open

web service - open

wfs / wps - open

jMeter Tests - open

agglomerative hierarchical clustering (cluster objects with childrens) - could be useful for clustering without nN - open

file saving with group of polygons - not only 1 polygon per file (rtree children [10]) - open

show envelops on map for rtree children - just show some stuff, to see comparison (clustering etc) - open

integrate osm api call to get xml file - xml->json convert - open

get rich - open
