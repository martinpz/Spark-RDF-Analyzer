var CONFIG = {
	prot : 'http',
	site : '127.0.0.1',
	port : '8080',
	rest : 'rest/ALL'
};

var REST_API = CONFIG.prot + "://" + CONFIG.site + ":" + CONFIG.port + "/"
		+ CONFIG.rest + "/";
