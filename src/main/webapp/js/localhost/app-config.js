const CONFIG = {
	prot : 'http',
	site : '127.0.0.1',
	port : '8080',
	rest : 'spark-rdfanalyzer2/rest/ALL'
};

const REST_API = CONFIG.prot + "://" + CONFIG.site + ":" + CONFIG.port + "/"
		+ CONFIG.rest + "/";

const NEW_GRAPH = {
	label: 'The local path to the folder that contains the graph data',
	examplePath: 'e.g. /home/data/dbpedia',
	exampleName: 'e.g. DBpedia'
}
