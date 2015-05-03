import scala.xml._;
import scala.xml.Elem._;
import scala.collection.immutable._;
import java.io._;


/** a class to extract tag data from XML files from a given list of tags */
class XMLParser {
	protected var file : Elem = null.asInstanceOf[Elem];
	
	/** sets the file object by converting the string to an xml.Elem object */
	def setFile(text : String) = { file = XML.loadString(text)}

	/** returns a sequence of CompanyEntity objects */
	def getCompanyEntities() : List[CompanyEntity] = {
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

}