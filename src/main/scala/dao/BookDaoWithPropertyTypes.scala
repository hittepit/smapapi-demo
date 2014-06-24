package dao

import org.hittepit.smapapi.transaction.JdbcTransaction
import org.hittepit.smapapi.transaction.TransactionManager
import org.hittepit.smapapi.core.PropertyType
import model.BookType
import model.BookType._
import java.sql.PreparedStatement
import java.sql.ResultSet
import org.slf4j.LoggerFactory
import org.hittepit.smapapi.core.StringProperty
import model.Isbn
import model.Book
import org.hittepit.smapapi.core.result.Row
import org.hittepit.smapapi.core.OptionalStringProperty
import org.hittepit.smapapi.core.OptionalIntProperty
import org.hittepit.smapapi.core.Param
import org.hittepit.smapapi.core.IntProperty
import org.hittepit.smapapi.core.Column

object BookTypePropertyType extends PropertyType[BookType]{
	def getColumnValue(rs: ResultSet, column: Either[String, Int]): BookType = BookType.withName(StringProperty.getColumnValue(rs, column))
	def setColumnValue(index: Int, value: BookType, ps: PreparedStatement): Unit= StringProperty.setColumnValue(index, value.toString, ps)
}

object IsbnPropertyType extends PropertyType[Isbn]{
	def getColumnValue(rs: ResultSet, column: Either[String, Int]): Isbn = Isbn(StringProperty.getColumnValue(rs, column))
	def setColumnValue(index: Int, value: Isbn, ps: PreparedStatement): Unit= StringProperty.setColumnValue(index, value.code, ps)
}

class BookDaoWithPropertyTypes(val transactionManager:TransactionManager) extends JdbcTransaction {
	val logger = LoggerFactory.getLogger(classOf[BookDaoWithPropertyTypes])

	def bookMapper(row:Row) = {
	  new Book(row.getColumnValue("ID", OptionalIntProperty), 
	      row.getColumnValue("TITLE", StringProperty), 
	      row.getColumnValue("ISBN", IsbnPropertyType), 
	      row.getColumnValue("AUTHOR", OptionalStringProperty), 
	      row.getColumnValue("BOOK_TYPE", BookTypePropertyType))
	}
	
	def find(id:Int) = readOnly{session =>
	  session.unique("select * from BOOK where id=?", List(Param(id,IntProperty)), bookMapper)
	}

	def findAll = readOnly{session =>
	  session.select("select * from Book",List()) map bookMapper
	}

	def persist(book:Book) = inTransaction{session =>
	  require(! book.id.isDefined)
  	  val id = session.insert("insert into book (title,isbn,author,book_type) values (?,?,?,?)", 
  	    List(Param(book.title,StringProperty),
  	        Param(book.isbn,IsbnPropertyType),
  	        Param(book.author,OptionalStringProperty),
  	        Param(book.bookType,BookTypePropertyType)), 
  	    Column("id",IntProperty))
  	  id match {
  	  	case Some(i) => new Book(Some(i),book.title,book.isbn,book.author,book.bookType)
  	  	case None => throw new Exception("No id generated")
  	  }
	}
	
	def update(book:Book) = inTransaction{session =>
	  require(book.id.isDefined)
  	  session.execute("update book set title=?, isbn=?, author=?, book_type=? where id=?",
  	      List(Param(book.title,StringProperty),
  	          Param(book.isbn,IsbnPropertyType),
  	          Param(book.author,OptionalStringProperty),
  	          Param(book.bookType,BookTypePropertyType),
  	          Param(book.id.get,IntProperty)))
  	  book
	}
	
	def delete(book:Book) = inTransaction{session =>
	  book.id match {
	    case Some(id) => session.execute("delete book where id=?",List(Param(id,IntProperty)))
	    case None => throw new Exception("Transient book")
	  }
	}}