var requester = {};
var geoJSON;
/**
 * Method to call requestController servlet and get json string.
 */
requester.dataBaseRequest = function(mode, minx, miny, maxx, maxy,scale,maxTypify,fixElements,minArea,minDistance,speed,typmode,weight,union,  callback) {	 
	$.ajax({
		"url" : 'mainServlet',
		"type" : 'GET',
		"data" : {
			"mode" : mode,
			"minx" : minx,		
			"miny" : miny,	
			"maxx" : maxx,	
			"maxy" : maxy,	
			"scale" : scale,
			"maxTypify" :maxTypify,
			"fixElements" : fixElements,
			"minArea" : minArea,
			"minDistance" : minDistance,
			"speed" : speed,
			"typmode" : typmode,
			"weight" : weight,
			"union" : union
		}, "success" : function(data,status) { 
			return callback(data);
		}
	});
};


/**
 * trash stuff
 */
requester.display = function(data) {
	
	geoJSON = $.parseJSON(data);
	//jsonObject = $.parseJSON(data);
	//console.log(jsonObject.features.length);
	//console.log(jsonObject.features[1].geometry.coordinates[0][0]);

};

requester.getJSON = function(data) {
	geoJSON = $.parseJSON(data);
	return geoJSON;
};