adhocGeneralize - Prototype
===============
with str-tree, selection, aggregation, typification, simplify and cluster hierarchy.
===============


Master Thesis - Bernd Grafe
TU Dresden » Faculty of Environmental Sciences Department of Geosciences » Institute for Cartography
"Usage of reactive data structures for automated generalisation of isolated polygons"


CONTENT
1. INSTALL
2. IMPORT PROJECT
3. START
4. VIEW
5. CHANGE DATA
6. PARAMETERS/REQUEST
7. JAVADOC

#######################

# 1. INSTALL

Download Eclipse JavaEE
https://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/lunasr1

Download Apache Tomcat
http://tomcat.apache.org/download-70.cgi

#######################

# 2. IMPORT PROJECT

Download Project Zip-File and import to your workspace, open properties of project, 
go to  project facets and choose Java, JavaScript and Dynamic Web Module.

or

via git - import project from git repository with url: https://github.com/Ahamann/adhocGeneralize.git
save as general project, open properties of project, go to  project facets and 
choose Java, JavaScript and Dynamic Web Module.

create a folder at C:\GenData - this is the folder, where your geojson should be stored
(this webapp start by default with lakesBetter.geojson - copy this file from
WebContent/data/lakesBetter.geojson to C:\GenData\lakesBetter.geojson)

(you can change the path in main.save.Container)

#######################

# 3. START

run as: Run on Server - choose Apache Tomcat - set your path or let Eclipse install a new version.
(Hint double click on your server in Servers-Tab to change timeout time to something high because
creating cluster hierarchy and reading files can take some time - min 3min)
(Hint 2 - change the number of thread used to read a file if necessary main.production.io.GeoJsonreader readFile2() )

#######################

# 4. VIEW

go to http://localhost:8080/[projectName] , where [projectName] ist most likely "adhocGeneralize"
index.html - single view
doubleView - double view with generalized map left and normal map right

#######################

# 5. CHANGE DATA

to change the input data, you need to drop you geojson-file to C:\GenData\
(e.g. copy sweden_lakes.geojson from WebContent/data/ to C:\GenData)
change the Path in main.save.Container to your new GeoJSON-File
change the start view in your index.html, there are presets for lakesBetter and sweden_lakes
for doubleView.html you need to change the path here to.

#######################

# 6. PARAMETERS/REQUEST

your url looks like this http://localhost:8080/[projectName]?parameter=value&parameter=value ...
possible paremeters are listed below

* mode = integer number from 0-9 (more information below), default = 0
* scale = double number for scale as 1:x, default = calculated based on ppi, latitude and zoom level
* speed = transfer rate in kbps, default = 320
* typify = maximum steps to typify (cluster to objects), default = 30
* elements = total elements maximum shown, default = calculated via radical law based on scale
* area = edge of square to calculate min area of poylgons to show on map in map[m] default = 0.001 (1mm)
* distance = max distance to merge polygons, default = 0.0005 (0.5mm)
* save = saves GeoJSON response from map if set to 1, default = 0
* typmode = modus of typification 0=create new str-tree after nN+cluster or 1=replace in same str-tree, default = 0
* weight = nN calculation based on distance and orientation with 1:x, default = 0
* union = integer number of maximum steps to merge polygons 0...x, default = -1 (deactivated)
* simplify = 1 (on) or 0 (off) - simplifies polygons if calculated transfer time is above 5 seconds, used for mode 5,6 and 9; default = 1
* map = 01 (on) or 0 (off) - show background map, default = 0

Mode:
* 0 = all without any generalization (ungeneralized data)
* 1 = selection - max Elements
* 2 = old bigger gets bigger typification (not recommended)
* 3 = typification
* 4 = pre-selection,typification, overlaps merge
* 5 = pre-selection, typification, overlaps merge, min area selection
* 6 = pre-select, union with max dist, typification, overlaps merge, min area selection (recommended)
* 7 = mode 5, shows diameter and supporting segments
* 8 = mode 0, shows diameter and supporting segments
* 9 = use pre processed cluster hierarchy (recommended)

#######################

# 7. JACADOC

[ProjectName]/Javadoc/index.html
