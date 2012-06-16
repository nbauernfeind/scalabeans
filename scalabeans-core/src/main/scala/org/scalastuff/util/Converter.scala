package org.scalastuff.util

trait Converter[A, B] {
  def to(a: A): B
  def from(b: B): A
  
  def compose[C](other: Converter[B, C]) = new Converter[A, C] { 
    def to(a: A) = other.to(Converter.this.to(a))
    def from(c: C) = Converter.this.from(other.from(c))
  }
}

object Converter {
  def apply[A, B](_to: A => B, _from: B => A) = new Converter[A, B] {
    def to(a: A) = _to(a)
    def from(b: B) = _from(b)
  }
  
  def identity[A] = new Converter[A, A] {
    def to(a: A) = a
    def from(b: A) = b
  } 
  
  implicit def format2convertor[A](fmt: Format[A]) = new Converter[A, Array[Byte]] {
    def to(a: A) = fmt.toByteArray(a)
    def from(b: Array[Byte]) = fmt.readFrom(b)
  }
}