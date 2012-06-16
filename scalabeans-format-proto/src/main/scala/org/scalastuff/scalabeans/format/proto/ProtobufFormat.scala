package org.scalastuff.scalabeans.format.proto
import java.io.InputStream
import org.scalastuff.proto.value.BeanValueHandler
import com.dyuproject.protostuff.CodedInput
import com.dyuproject.protostuff.ByteArrayInput
import org.scalastuff.scalabeans.types.TupleType
import org.scalastuff.scalabeans.types.ScalaType
import org.scalastuff.util.Format
import java.io.OutputStream
import com.dyuproject.protostuff.ProtobufIOUtil
import com.dyuproject.protostuff.Schema


class ProtobufFormat[T] private[proto](beanValueHandler: BeanValueHandler) extends Format[T] {
  
  def readFrom(buffer: Array[Byte]): T = {
    val input = new ByteArrayInput(buffer, false)
    beanValueHandler.beanReadFrom(input).asInstanceOf[T]
  }
  
  def readFrom(inputStream: InputStream): T = {
    val input = new CodedInput(inputStream, false)
    beanValueHandler.beanReadFrom(input).asInstanceOf[T]
  }
  
  def writeTo(outputStream: OutputStream, bean: T) = {
    val buffer = BufferPool.getLinkedBuffer()
    ProtobufIOUtil.writeTo(outputStream, bean, writeSchema, buffer)
  }
  
  def toByteArray(bean: T): Array[Byte] = {
    val buffer = BufferPool.getLinkedBuffer()
    ProtobufIOUtil.toByteArray(bean, writeSchema, buffer)
  }
  
  private val writeSchema = beanValueHandler.writeSchema.asInstanceOf[Schema[T]]
}

class WrappedProtobufFormat[T] private[proto](scalaType: ScalaType) extends Format[T] {
  def readFrom(buffer: Array[Byte]): T = wrappedFormat.readFrom(buffer)._1
  
  def readFrom(inputStream: InputStream): T = wrappedFormat.readFrom(inputStream)._1
  
  def writeTo(outputStream: OutputStream, bean: T) = wrappedFormat.writeTo(outputStream, Tuple1(bean))
  
  def toByteArray(bean: T): Array[Byte] = wrappedFormat.toByteArray(Tuple1(bean))
  
  val wrappedSchema = BeanValueHandler(TupleType(scalaType)).writeSchema.asInstanceOf[Schema[Tuple1[T]]]
  
  private val wrappedValueHandler = BeanValueHandler(TupleType(scalaType))
  private val wrappedFormat = new ProtobufFormat[Tuple1[T]](wrappedValueHandler)
}