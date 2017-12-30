class Answer::FreeText < Answer

  def is_correct_answered_with?(answer)
    puts "Korrekte Antwort: #{self.correct_answer_string}"
    puts "Antwort #{answer}"

    ld = levenshtein_distance(self.correct_answer_string.strip.downcase, answer.downcase)
    if ld <= 1
      return true
    else
      return false
    end
  end

  
end
