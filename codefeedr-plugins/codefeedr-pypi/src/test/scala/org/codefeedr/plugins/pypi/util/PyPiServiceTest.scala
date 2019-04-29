package org.codefeedr.plugins.pypi.util

import java.util.UUID

import org.scalatest.FunSuite

class PyPiServiceTest extends FunSuite {

  test("Retrieve project") {
    val projectName = "urllib3" //most popular pypi project

    val project = PyPiService.getProject(projectName)

    assert(!project.isEmpty)
    assert(project.get.info.name == "urllib3")
  }

  test("Retrieve non-existing project") {
    val projectName = UUID.randomUUID().toString

    val project = PyPiService.getProject(projectName)

    assert(project.isEmpty)
  }
}
