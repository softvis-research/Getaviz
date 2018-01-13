json.array!(@surveys) do |survey|
  json.extract! survey, :id, :name, :description
  json.url survey_url(survey, format: :json)
end
