package org.codefeedr.plugins

import org.apache.flink.streaming.api.scala.{DataStream, _}
import org.codefeedr.pipeline.{NoType, PipelineItem, PipelineObject}

case class StringType(value: String) extends PipelineItem

class StringSource(str : String = "") extends PipelineObject[NoType, StringType] {

  override def transform(source: DataStream[NoType]): DataStream[StringType] = {

    val text = if (str == "") """Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris augue eros, venenatis sed lorem nec, venenatis feugiat neque. Cras bibendum libero a nunc ultricies, id varius mauris varius. Nulla ac convallis lacus, volutpat volutpat turpis. Sed eu metus vitae sapien lacinia cursus a sed dui. Aenean egestas consequat metus, ac sollicitudin eros lobortis eu. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Morbi gravida, justo a malesuada venenatis, dui ipsum ultricies ante, et blandit mauris diam quis augue. Maecenas enim lorem, vulputate et rutrum sit amet, vehicula eget nulla.
                 |Duis vitae felis et ex porta vestibulum rhoncus in dui. Integer arcu lectus, interdum in vestibulum sed, congue et eros. Quisque at gravida libero. Morbi et pulvinar odio. Maecenas augue odio, sodales in dolor ut, pretium blandit enim. Etiam condimentum cursus dictum. Ut egestas nibh ac enim posuere, et scelerisque lectus pellentesque. Aenean lobortis quis velit eu vehicula. Donec sit amet justo a eros vestibulum pharetra non ac nunc. Nunc pretium, turpis in aliquam rhoncus, mi turpis ultricies dui, vitae dapibus odio dui finibus leo. Ut euismod felis vel lorem commodo, eget interdum nisl imperdiet.
                 |Cras vitae mattis urna. Pellentesque cursus ligula finibus dui tristique lobortis. Aenean finibus odio lectus. Etiam lacus nibh, facilisis vel urna id, ultrices sagittis velit. Integer vulputate nisi eget odio feugiat accumsan. Morbi sodales lobortis lorem, at congue enim venenatis nec. In gravida quam quis urna tempus, eu fermentum orci porttitor. Etiam at nisi vehicula, euismod nibh semper, dignissim ante. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.
                 |Proin eu rhoncus nulla. Etiam gravida erat eleifend diam varius pulvinar. Morbi iaculis, justo iaculis dignissim dictum, nulla libero euismod ex, vel ultrices neque urna eu ante. Nulla mattis, metus non lobortis malesuada, urna nulla ullamcorper dolor, id elementum mi justo in enim. Maecenas sollicitudin ex dui, nec volutpat tortor sagittis nec. Maecenas bibendum sapien nec malesuada consectetur. Nulla ac lacus vitae ex tempor finibus. Nunc turpis arcu, porttitor nec rutrum eget, sagittis in purus. Morbi aliquam, felis vel sollicitudin tincidunt, elit neque interdum leo, id blandit ex sapien a turpis. Phasellus tincidunt risus non erat efficitur imperdiet. Duis finibus, diam vitae vestibulum tempus, neque turpis malesuada lacus, vel pretium elit leo ut ante.
                 |Vivamus dignissim justo sed ante viverra pretium. Quisque tristique, risus sit amet feugiat laoreet, orci velit molestie nunc, ac interdum nunc massa in nisl. Vestibulum malesuada id lorem vitae laoreet. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Quisque venenatis eros a mollis euismod. Praesent tincidunt diam nec accumsan sodales. Proin ut diam et turpis posuere condimentum ut a tortor. Etiam eu mattis odio. Curabitur vel orci vitae massa finibus fermentum vel vitae arcu. Etiam eu nibh gravida, malesuada ligula in, cursus ipsum.""" else str

    val list = text.split("[ \n]")

    pipeline.environment
      .fromCollection(list)
      .map { str => StringType(str) }
  }

}