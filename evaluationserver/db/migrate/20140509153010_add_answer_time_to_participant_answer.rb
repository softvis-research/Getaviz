class AddAnswerTimeToParticipantAnswer < ActiveRecord::Migration
  def change
    add_column :participant_answers, :time_needed_in_ms, :integer
  end
end
