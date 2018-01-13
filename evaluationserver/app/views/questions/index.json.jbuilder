json.array!(@questions) do |question|
  json.extract! question, :id, :question_text, :question_type_id
  json.url question_url(question, format: :json)
end
