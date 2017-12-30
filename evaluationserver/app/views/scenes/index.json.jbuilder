json.array!(@scenes) do |scene|
  json.extract! scene, :id, :name, :description
  json.url scene_url(scene, format: :json)
end
