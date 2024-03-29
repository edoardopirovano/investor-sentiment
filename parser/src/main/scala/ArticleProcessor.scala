import scala.xml._;
import scala.xml.Elem._;
import scala.xml.Node._;
import org.joda.time.DateTime
import javax.xml.transform.sax.SAXResult;
import scala.xml.parsing.NoBindingFactoryAdapter;
import com.typesafe.config.ConfigFactory;
import com.alchemyapi.api.AlchemyAPI
import com.alchemyapi.api._
import org.w3c.dom.Document;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/** a class to extract tag data from XML files from a given list of tags */
object ArticleProcessor {
	val conf = ConfigFactory.load();	
	val alchemyKey = conf.getString("apikeys.alchemy");
	val alchemyObj = AlchemyAPI.GetInstanceFromString(alchemyKey);
	val sentimentParams = new AlchemyAPI_TargetedSentimentParams(); // a paramter object 
	
	def processArticle(url: String, stock: String): (Int, Int) = { // Importance, sentiment
		if (url contains "video") throw new IllegalArgumentException(url+" is a video")
		try {
			val alchemyResult = asXml(alchemyObj.URLGetTargetedSentiment(url,stock,sentimentParams));
			val importance = SiteRank.getPopularity(url);
			return (importance, Math.round((1+getSentimentScore(alchemyResult))*50));
		} catch {
			case e : Exception => throw new IllegalArgumentException(stock+" couldn't be found in "+url);
		}
	}
	
	/** finds the sentiment score within the XML document */
	def getSentimentScore(file: Node) : Float = {
		return (file \\ "score")(0).text.toFloat;
	} 
	
	def asXml(dom: _root_.org.w3c.dom.Node): Node = {
	  val source = new DOMSource(dom)
	  val adapter = new NoBindingFactoryAdapter
	  val saxResult = new SAXResult(adapter)
	  val transformerFactory = javax.xml.transform.TransformerFactory.newInstance()
	  val transformer = transformerFactory.newTransformer()
	  transformer.transform(source, saxResult)
	  adapter.rootElem
  }
}