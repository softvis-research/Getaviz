class Answer::Slider < Answer
  def is_correct_answered_with?(answer)
    puts "Korrekte Antwort: #{self.correct_answer_value_min}..#{self.correct_answer_value_max}"
    puts "Antwort #{answer}"

    answer_float = answer.to_f
    return (self.correct_answer_value_min.to_f..self.correct_answer_value_max.to_f).include?(answer_float)
  end
  
  
end
