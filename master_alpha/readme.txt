ad hoc realtime generalization with javascript and java.



	
front end:	
temp.html->requester.js->Servlet.java->RequestHandler.java


to do:

read file - check
convert to polygon - check
write json file - check
visualization of self created json - check
r- tree (save polygons per file/per extent)- check
selection based on extent/zoom - check 
selection operator/delete smallest polygons based on extent - check
typification based on area (nearest neighbors of biggest area - get extent, buffer biggest to extent, delete remaining polygons) - more or less check
->Problem - its more like an absorption - sort polygons(area) - get neighbors via extent expansion- delete them, increase polygon

new nearest neighbour - fixed jts classes for proper nN method + remove methode + new replace method for clustering
only prototype




quad tree - 
simplification douglas-peucker/visvalingam - 
aggregation/amalgamation, polygons with same outline - 
aggregation, polygons based on distance - 

based on tgap - save polygon for different zoomlevels, not only highest level - 

speed test / Junit - 

test for same small data size which will be send - 

create different client sets ( thin for speed - op changer - view comparison orig-tree ) - 

big data tests - 

topology checks - 

