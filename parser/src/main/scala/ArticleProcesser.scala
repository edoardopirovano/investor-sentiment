import scala.xml._;
import scala.xml.Elem._;
import scala.xml.Node._;
import scala.collection.immutable._;
import java.io._;
import org.joda.time.DateTime
import javax.xml.transform.sax.SAXResult;
import scala.xml.parsing.NoBindingFactoryAdapter;
import org.xml.sax.SAXException;
import com.typesafe.config.ConfigFactory;
import com.alchemyapi.api.AlchemyAPI
import com.alchemyapi.api._
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import java.io._;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/** a class to extract tag data from XML files from a given list of tags */
object ArticleProcesser {
	val conf = ConfigFactory.load();	
	val alchemyKey = conf.getString("apikeys.alchemy");
	val alchemyObj = AlchemyAPI.GetInstanceFromString(alchemyKey);
	/** an object to allow additional 'NamedEntity' parameters to be specified during API calls */
	val entityParams : AlchemyAPI_NamedEntityParams = new AlchemyAPI_NamedEntityParams();
	/** give a sentiment analysis for each entity found in an API query using entityParams */
	entityParams.setSentiment(true);
	def processArticle(url: String, stock: String): (Int, Int) = { // Importance, sentiment
		val importance = SiteRank.getPopularity(url)
		val alchemyResult = asXml(alchemyObj.URLGetRankedNamedEntities(url,entityParams));
		println(alchemyResult);
		val info = getCompanyEntities(alchemyResult);
		for (entity <- info) {
			if (entity.name contains stock) return (importance, Math.round((1+entity.sentiment).toFloat*50))
		}
		return (0,0)
	}
	/** returns a sequence of CompanyEntity objects */
	private def getCompanyEntities(file: Node) : List[CompanyEntity] = {
		var companyEntities = List[CompanyEntity]();
		// split the file, a node for each entity
		val entities = file \\ "entity"; 
		for (entity <- entities) {
			// is this entity a company?
			if ((entity \ "type")(0).text == "Company") {
				val compEnt = new CompanyEntity();
				compEnt.name = (entity \\ "text")(0).text;
				compEnt.sentiment = (entity \\ "score")(0).text.toDouble;
				compEnt.relevance = (entity \\ "relevance")(0).text.toDouble;
				println(compEnt);
				companyEntities = compEnt :: companyEntities;
			}
		} 
		return companyEntities;
	}
	private def asXml(dom: _root_.org.w3c.dom.Node): Node = {
	  val source = new DOMSource(dom)
	  val adapter = new NoBindingFactoryAdapter
	  val saxResult = new SAXResult(adapter)
	  val transformerFactory = javax.xml.transform.TransformerFactory.newInstance()
	  val transformer = transformerFactory.newTransformer()
	  transformer.transform(source, saxResult)
	  adapter.rootElem
  }
}