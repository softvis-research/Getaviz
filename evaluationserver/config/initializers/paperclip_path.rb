if Rails.env.production?
  ATTACHMENT_BASE_PATH = ENV['OPENSHIFT_DATA_DIR']
else
  ATTACHMENT_BASE_PATH = "#{Rails.root.to_s}/local/"
end

Paperclip::Attachment.default_options[:path] = "#{ATTACHMENT_BASE_PATH}/:class/:attachment/:id_partition/:filename"
