ad hoc realtime generalization with javascript and java.


main.controller
	Servlet.java
	RequestHandler.java

main.production
	Factory.java
	PolygonWorker.java

main.production.reader
	GeoJsonReader.java
	
main.production.writer
	GeoJsonWriter.java

main.runnable.java
	Tester.java
	

	
front end:	
temp.html->requester.js->Servlet.java->RequestHandler.java


to do:

read file - check
convert to polygon - check
write json file - check
visualization of self created json - check

r- tree (save polygons per file/per extent)- check
quad tree - 
selection based on extent/zoom - check 
selection operator/delete smallest polygons based on extent - 
typification based on area (nearest neighbors of biggest area - get extent, buffer biggest to extent, delete remaining polygons) - 

simplification douglas-peucker/visvalingam - 
aggregation/amalgamation, polygons with same outline - 
aggregation, polygons based on distance - 

based on tgap - save polygon for different zoomlevels, not only highest level - 

speed test / Junit - 

test for same small data size which will be send - 

create different client sets ( thin for speed - op changer - view comparison orig-tree ) - 

big data tests - 

topology checks - 

