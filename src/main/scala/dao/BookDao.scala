package dao

import org.hittepit.smapapi.transaction.JdbcTransaction
import org.hittepit.smapapi.transaction.TransactionManager
import org.slf4j.LoggerFactory
import org.hittepit.smapapi.core.result.Row
import org.hittepit.smapapi.core.NotNullableString
import model.BookType
import org.hittepit.smapapi.core.NullableInt
import model.Book
import org.hittepit.smapapi.core.NullableString
import org.hittepit.smapapi.core.Param
import org.hittepit.smapapi.core.NotNullableInt
import model.Isbn
import org.hittepit.smapapi.core.Column

class BookDao(val transactionManager:TransactionManager) extends JdbcTransaction{
	val logger = LoggerFactory.getLogger(classOf[BookDao])
	
	def bookMapper(row:Row) = {
	  val bookType = BookType.withName(row.getColumnValue("BOOK_TYPE", NotNullableString))
	  new Book(row.getColumnValue("ID", NullableInt), 
	      row.getColumnValue("TITLE", NotNullableString), 
	      Isbn(row.getColumnValue("ISBN", NotNullableString)), 
	      row.getColumnValue("AUTHOR", NullableString), 
	      bookType)
	}
	
	def find(id:Int) = readOnly{session =>
	  session.unique("select * from BOOK where id=?", List(Param(id,NotNullableInt)), bookMapper)
	}
	
	def findAll = readOnly{session =>
	  session.select("select * from Book",List()) map bookMapper
	}
	
	def saveOrUpdate(book:Book) = inTransaction{session =>
	  book.id match {
	  	case None => 
	  	  val id = session.insert("insert into book (title,isbn,author,book_type) values (?,?,?,?)", 
	  	    List(Param(book.title,NotNullableString),Param(book.isbn.code,NotNullableString),Param(book.author,NullableString),Param(book.bookType.toString,NotNullableString)), 
	  	    Column("id",NotNullableInt))
	  	  id match {
	  	  	case Some(i) => new Book(Some(i),book.title,book.isbn,book.author,book.bookType)
	  	  	case None => throw new Exception("No id generated")
	  	  }
	  	  
	  	case Some(id) => 
	  	  session.execute("update book set title=?, isbn=?, author=?, book_type=? where id=?",
	  	      List(Param(book.title,NotNullableString),
	  	          Param(book.isbn.code,NotNullableString),
	  	          Param(book.author,NullableString),
	  	          Param(book.bookType.toString,NotNullableString),
	  	          Param(book.id.get,NotNullableInt)))
	  	  book
	  }
	}
	
	def delete(book:Book) = inTransaction{session =>
	  book.id match {
	    case Some(id) => session.execute("delete book where id=?",List(Param(id,NotNullableInt)))
	    case None => throw new Exception("Transient book")
	  }
	}
}