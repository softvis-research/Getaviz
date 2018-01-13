class AddAnswerTimeToParticipantAnswer < ActiveRecord::Migration[4.2]
  def change
    add_column :participant_answers, :time_needed_in_ms, :integer
  end
end
