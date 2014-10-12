require 'rake'
require 'pathname'
require 'yaml'
require 'net/http'
require 'logger'

def project_root
  @project_root ||= Pathname.new(File.dirname(__FILE__))
end

def project_path *args
  project_root.join(*args)
end

def environment
  ENV["PLUTUS_ENV"] || "dev"
end

def all_config
  @all_config ||= YAML.load(open(project_path('config/plutus.yml')))
end

def config
  all_config[environment]
end

def log
  @log ||= Logger.new(STDERR)
end

def command *args
  cmd = args.join(' ')
  log.info(cmd)
  system cmd
end

def http_request verb, host_and_port, path, body=nil, headers={}
  connection = Net::HTTP.new(*host_and_port.split(":"))
  request    = Net::HTTP.const_get(verb.to_s.capitalize).new(path, headers)
  request.body = body if body
  log.info "#{verb.to_s.upcase} http://#{File.join(host_and_port, path)}"
  response = connection.request(request)
  puts response.body
end

def es verb, path, body=nil, headers={}
  host, port = config['elasticsearch']['hosts'].sample.split(':')
  http_request verb, "#{host}:#{port || 9200}", path, body, headers
end

def cql *args
  host, port = config['cassandra']['hosts'].sample.split(':')
  query = args.join(' ').gsub("'", "\\\\'")
  command "cqlsh -e '#{query}' #{host} #{port || 9042}"
end

def gremlin path
  command "gremlin -e #{path}"
end

task :env do
end

namespace :elasticsearch do
  desc "Destroy the Elasticsearch index for plutus"
  task :destroy => [:env] do
    es :delete, "/#{config['elasticsearch']['index']}"
  end
end

namespace :cassandra do
  desc "Destroy the Cassandra keyspace for plutus"
  task :destroy => [:env] do
    cql "DROP KEYSPACE #{config['cassandra']['keyspace']}"
  end
end

namespace :titan do
  desc "Create the TitanDB graph schema"
  task :schema => [:env] do
    gremlin project_path("config/schema.groovy")
  end
end

desc "Destroy all data!"
task :destroy => ["elasticsearch:destroy", "cassandra:destroy"]
