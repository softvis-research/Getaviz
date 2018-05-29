class Answer < ApplicationRecord
  belongs_to :answer_possibility_collection

  
  before_save :set_type

  def set_type
    case self.answer_possibility_collection.answer_type
    when "freetext"
      self.type = 'Answer::FreeText'
    when "slider"
      self.type = 'Answer::Slider'
    when "multiplechoice"
      self.type = 'Answer::MultipleChoice'
    when "choice"
      self.type = 'Answer::Choice'
    end
  end


  ## override in subclasses
  def is_correct_answered_with?(answer)
    return false
  end



  ### http://stackoverflow.com/questions/16323571/measure-the-distance-between-two-strings-with-ruby
  def levenshtein_distance(s, t)
    m = s.length
    n = t.length
    return m if n == 0
    return n if m == 0
    d = Array.new(m+1) {Array.new(n+1)}
    
    (0..m).each {|i| d[i][0] = i}
    (0..n).each {|j| d[0][j] = j}
    (1..n).each do |j|
      (1..m).each do |i|
        d[i][j] = if s[i-1] == t[j-1]  # adjust index into string
                    d[i-1][j-1]       # no operation required
                  else
                    [ d[i-1][j]+1,    # deletion
                      d[i][j-1]+1,    # insertion
                      d[i-1][j-1]+1,  # substitution
                    ].min
                  end
      end
    end
    d[m][n]
  end




  
end
