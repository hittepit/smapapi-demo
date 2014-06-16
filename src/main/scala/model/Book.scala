package model

import org.apache.commons.validator.routines.ISBNValidator

object BookType extends Enumeration{
  type BookType = Value
  
  val eBook = Value("eBook")
  val paper = Value("paper")
}

import BookType._

class Isbn(c:String){
  require(Isbn.validate(c))
  val code= new ISBNValidator(true).validate(c)
}

object Isbn{
  def apply(code:String) = new Isbn(code)
  def validate(code:String) = new ISBNValidator(true).isValid(code)
}

class Book(val id:Option[Int], val title:String, val isbn:Isbn, val author:Option[String], val bookType:BookType)