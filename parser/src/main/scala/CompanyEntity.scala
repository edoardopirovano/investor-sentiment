/** a simple class to encapsulate an Entity and its data */
class CompanyEntity {
	var name : String = "DEFAULT";
	var sentiment : Double = 0.0;
	var relevance : Double = 0.0;
	override def toString() : String = "(name = "+name+", sentiment = "+sentiment+", relevance = "+relevance+")"
}