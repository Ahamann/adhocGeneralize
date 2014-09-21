var requester = {};
var geoJSON;
/**
 * Method to call requestController servlet and get json string.
 */
requester.dataBaseRequest = function(mode, minx, miny, maxx, maxy,zoom,  callback) {	 
	$.ajax({
		"url" : 'mainServlet',
		"type" : 'GET',
		"data" : {
			"mode" : mode,
			"minx" : minx,		
			"miny" : miny,	
			"maxx" : maxx,	
			"maxy" : maxy,	
			"zoom" : zoom
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