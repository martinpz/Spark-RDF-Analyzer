const CONFIG = {
	prot: 'http',
	site: 'isydney.informatik.uni-freiburg.de',
	port: '8080',
	rest: 'spark-rdfanalyzer2/rest/ALL'
};

const REST_API = CONFIG.prot + "://" + CONFIG.site + ":" + CONFIG.port + "/" +
	CONFIG.rest + "/";

const NEW_GRAPH = {
	label: 'The HDFS path to the folder that contains the graph data',
	examplePath: 'e.g. hdfs://sydney.informatik.privat:8020/user/teamproject2016/data/dbpedia',
	exampleName: 'e.g. DBpedia'
}
