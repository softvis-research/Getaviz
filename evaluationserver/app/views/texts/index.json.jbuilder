json.array!(@texts) do |text|
  json.extract! text, :id, :html
  json.url text_url(text, format: :json)
end
