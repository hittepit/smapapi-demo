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
import org.hittepit.smapapi.core.Transient
import org.hittepit.smapapi.core.Persistent
import org.hittepit.smapapi.core.Persistent

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
        book.id must be (Persistent(1000))
        book.title must be("Dune")
        book.author must be (Some("Frank Herbert"))
        book.isbn.code must be("9780450011849")
        book.bookType must be(BookType.eBook)
      }
      "return an option with the correct Book object with None as author if author was not defined" in {
        val b = bookDao.find(1001)
        b must be('defined)
        val book = b.get
        book.id must be (Persistent(1001))
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
        val b1 = new Book(Transient(),"Histoire d'automates",Isbn("2253033723"),None,BookType.eBook)
        
        val b2 = bookDao.saveOrUpdate(b1)
        
        b2 must not be theSameInstanceAs(b1)
        
        b2.id must be('generated)
      }
      "insert the book in database" in {
        val b1 = new Book(Transient(),"Histoire d'automates",Isbn("2253033723"),None,BookType.eBook)
        val b2 = bookDao.saveOrUpdate(b1)
        
        val b3 = bookDao.find(b2.id.value)
        
        b3 must be('defined)
        b3.get.title must be("Histoire d'automates")
        b3.get.isbn must be(Isbn("2253033723"))
        b3.get.author must be(None)
      }
    }
    "invoked with a persistent book" must {
      "return the same instance of book" in {
        val b1 = new Book(Persistent(1001),"Histoire de Robots",Isbn("2253071951"),Some("Klein"),BookType.paper)
        
        val b2 = bookDao.saveOrUpdate(b1)
        
        b2 must be theSameInstanceAs(b1)
      }
      "update the book in database" in {
        val b1 = new Book(Persistent(1001),"Histoire de Robots",Isbn("2253071951"),Some("Klein"),BookType.paper)
        bookDao.saveOrUpdate(b1)
        val b2 = bookDao.find(1001)
        
        b2.get.title must be("Histoire de Robots")
        b2.get.isbn must be(Isbn("2253071951"))
        b2.get.author must be(Some("Klein"))
        b2.get.bookType must be(BookType.paper)
      }
    }
  }
  
  "Delete" when {
    "called with a persitent book" must {
      "delete the row in DB corresponding to its id" in {
        val b = new Book(Persistent(1000),"Dune",Isbn("9780450011849"),None,BookType.paper)
        bookDao.delete(b)
        
        bookDao.find(1000) must be(None)
      }
      "return the number of deleted lines" in {
        val b = new Book(Persistent(1000),"Dune",Isbn("9780450011849"),None,BookType.paper)
        bookDao.delete(b) must be(1)
      }
    }
    "called with an non existing book" must {
      "return 0" in {
        val nonExitentBook = new Book(Persistent(15),"rien",Isbn("2253071951"),None,BookType.paper)
        bookDao.delete(nonExitentBook) must be(0)
      }
    }
    "called with a transient book" must {
      "throw an exception" in {
        val transientBook = new Book(Transient(),"rien",Isbn("2253071951"),None,BookType.paper)
        an [Exception] must be thrownBy(bookDao.delete(transientBook))
      }
    }
  }
}