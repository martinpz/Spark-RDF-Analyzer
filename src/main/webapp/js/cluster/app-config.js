var CONFIG = {
	prot : 'http',
	site : 'isydney.informatik.uni-freiburg.de',
	port : '8081',
	rest : 'spark-rdfanalyzer2/rest/ALL'
};

var REST_API = CONFIG.prot + "://" + CONFIG.site + ":" + CONFIG.port + "/"
		+ CONFIG.rest + "/";
