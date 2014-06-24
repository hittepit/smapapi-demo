package model

import org.apache.commons.validator.routines.ISBNValidator
import org.hittepit.smapapi.core.GeneratedId

object BookType extends Enumeration{
  type BookType = Value
  
  val eBook = Value("eBook")
  val paper = Value("paper")
}

import BookType._

class Isbn(c:String){
  require(Isbn.validate(c))
  val code= new ISBNValidator(true).validate(c)
  
  override def equals(o:Any) = o match {
    case i:Isbn => i.code == code
    case _ => false
  }
  
  override def hashCode = code.hashCode()
  
  override def toString = "Isbn("+code+")"
}

object Isbn{
  def apply(code:String) = new Isbn(code)
  def validate(code:String) = new ISBNValidator(true).isValid(code)
}

class Book(val id:GeneratedId[Int], val title:String, val isbn:Isbn, val author:Option[String], val bookType:BookType)