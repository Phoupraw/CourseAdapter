package main.java.parser

import bean.Course
import org.jsoup.Jsoup
import parser.Parser

//北京中医药大学 本科
class BUCMParser(source: String) : Parser(source) {

	override fun generateCourseList(): List<Course> {
		val doc = Jsoup.parse(source)
		val courseList = arrayListOf<Course>()
		var firstRow = true//第一行是星期，需要跳过
		for (row in doc.getElementById("timetable").getElementsByTag("TR")) {//逐行读取表格
			if (firstRow) {
				firstRow = false
				continue
			}
			for ((day, cell) in row.getElementsByTag("TD").withIndex()) {
				val kbcontents = cell.getElementsByClass("kbcontent")//不知道什么鬼的类，反正这里面储存着课程信息
				if (kbcontents.isEmpty())//如果无kbcontents元素，则非课程单元格
					continue
				val text = kbcontents[0]
				val teachers = text.getElementsByAttributeValue("title", "教师")
				if (teachers.isEmpty())//如果无教师，则为空单元格
					continue
				val childNodes = text.childNodes()
				var i = 0
				while (i < childNodes.size) {
					var name = ""        // 课程名
					var building = ""//教学楼
					var room = ""       // 教室
					var teacher = ""    // 老师
					var startNode = 0        // 开始为第几节课
					var endNode = 0         // 结束时为第几节课
					val startEndWeeks = arrayListOf<Pair<Int, Int>>()//每个分段开始和结束的星期
					var note = ""       // 备注
					while (i < childNodes.size && !childNodes[i].toString().contains("------")) {//------是分隔符
						val node = childNodes[i]
						val attributes = node.attributes()
						for (entry in attributes) when (entry.key) {
							"#text" -> name += entry.value
							"title" -> {
								val content = node.childNodes()[0].toString()
								when (entry.value) {
									"教师" -> teacher = content
									"周次(节次)" -> {
										val weekss = content.substringBefore("(周)[").split(",")
										for (weeksi in weekss) {
											val weeks = weeksi.split("-")
											startEndWeeks.add(Pair(weeks.first().toInt(), weeks.last().toInt()))
										}
										val nodes = content.substringAfter("(周)[").substringBefore("节]").split("-")
										startNode = nodes.first().toInt()
										endNode = nodes.last().toInt()
									}
									"教学楼" -> building = content
									"教室" -> room = content
								}
							}
							"name" -> if (entry.value == "xsks") note = node.childNodes()[0].toString()
						}
						i++
					}
					for (startEndWeek in startEndWeeks) {
						courseList.add(Course(name, day, "$building$room", teacher, startNode, endNode, startEndWeek.first, startEndWeek.second, 0, 0f, note, "", ""))
					}
					i++
				}
			}
		}
		return courseList
	}


}