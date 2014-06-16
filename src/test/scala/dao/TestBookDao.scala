package dao

import org.scalatest.WordSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.MustMatchers
import org.apache.commons.dbcp.BasicDataSource
import java.sql.Connection
import org.hittepit.smapapi.transaction.TransactionManager
import model.BookType
import model.Book
import model.Isbn

class TestBookDao extends WordSpec with BeforeAndAfter with MustMatchers{
 class DataSource extends BasicDataSource {
    this.setDriverClassName("org.h2.Driver")
    this.setUsername("h2")
    this.defaultAutoCommit = false
    this.defaultTransactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    this.setUrl("jdbc:h2:mem:test;MVCC=TRUE")
  }

  val ds = new DataSource

  val bookDao = new BookDao(TransactionManager(ds))
  
  before {
    val connection = ds.getConnection()
    var st = connection.createStatement
    st.addBatch("create table BOOK (ID integer auto_increment, TITLE varchar(50), ISBN VARCHAR(13), AUTHOR varchar(50), BOOK_TYPE varchar(5), PRIMARY KEY(id));")
    st.addBatch("insert into BOOK (id,title,isbn,author,book_type) values (1000,'Dune','9780450011849','Frank Herbert','eBook');")
    st.addBatch("insert into BOOK (id,title,isbn,author,book_type) values (1001,'Histoires de Robots','2253071951',null,'paper');")
    st.addBatch("insert into BOOK (id,title,isbn,author,book_type) values (1002,'La chute d''Hypérion','2266111566','Dan Simmons','paper');")
    st.executeBatch()
    connection.commit()
    connection.close
  }

  after {
    val connection = ds.getConnection()
    var st = connection.createStatement
    st.addBatch("delete from BOOK");
    st.addBatch("drop table BOOK;")
    st.executeBatch()
    connection.commit()
    connection.close
  }

  "The find method" when {
    "called with an existing id" must {
      "return an option with the correct Book object with the author if author was defined" in {
        val b = bookDao.find(1000)
        b must be('defined)
        val book = b.get
        book.id must be (Some(1000))
        book.title must be("Dune")
        book.author must be (Some("Frank Herbert"))
        book.isbn.code must be("9780450011849")
        book.bookType must be(BookType.eBook)
      }
      "return an option with the correct Book object with None as author if author was not defined" in {
        val b = bookDao.find(1001)
        b must be('defined)
        val book = b.get
        book.id must be (Some(1001))
        book.title must be("Histoires de Robots")
        book.author must be (None)
        book.isbn.code must be("9782253071952")
        book.bookType must be(BookType.paper)
      }
    }
    "called with an non-existing id" must {
      "return None" in {
        val b = bookDao.find(100000)
        b must be(None)
      }
    }
  }
  
  "The findAll method" when {
    "invoked" must {
      "return the list of all books" in {
        val books = bookDao.findAll
        books.size must be(3)
      }
    }
  }
  
  "The saveOrUpdate method" when {
    "invoked with a transient book" must {
      "return a persisted book" in {
        val b1 = new Book(None,"Histoire d'automates",Isbn("2253033723"),None,BookType.eBook)
        
        val b2 = bookDao.saveOrUpdate(b1)
        
        b2 must not be theSameInstanceAs(b1)
        
        b2.id must be('defined)
      }
      "insert the book in database" in {
        val b1 = new Book(None,"Histoire d'automates",Isbn("2253033723"),None,BookType.eBook)
        val b2 = bookDao.saveOrUpdate(b1)
        
        val b3 = bookDao.find(b2.id.get)
        
        b3 must be('defined)
        b3.get.title must be("Histoire d'automates")
        b3.get.isbn must be(Isbn("2253033723"))
        b3.get.author must be(None)
      }
    }
  }
}