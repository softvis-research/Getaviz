package org.svis.metrics.tests.helper

import java.util.List

class Utils {
	def static toLongList(List<Integer> input) {
		val result = newLinkedList
		input.forEach[el|
			result += Long::valueOf(el)
		]
		return result
	}
}