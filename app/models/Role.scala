package models

/**
 * Created by madalien on 26/04/15.
 */

sealed trait Role

object Role {

  case object Administrator extends Role
  case object NormalUser extends Role

  def valueOf(value: String): Role = value match {
    case "Administrator" => Administrator
    case "NormalUser"    => NormalUser
    case _ => throw new IllegalArgumentException()
  }

  def stringValueOf(value: Role): String = value match {
    case Administrator => "Administrator"
    case NormalUser => "NormalUser"
    case _ => throw new IllegalArgumentException()
  }


}
