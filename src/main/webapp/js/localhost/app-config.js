var CONFIG = {
	prot : 'http',
	site : '127.0.0.1',
	port : '8081',
	rest : 'spark-rdfanalyzer2/rest/ALL'
};

var REST_API = CONFIG.prot + "://" + CONFIG.site + ":" + CONFIG.port + "/"
		+ CONFIG.rest + "/";
