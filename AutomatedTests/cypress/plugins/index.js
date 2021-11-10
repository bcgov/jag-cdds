require('dotenv').config()

module.exports = (on, config) => {
  // copy any needed variables from process.env to config.env
  config.env.cdds_host = process.env.WEB_METHODS_HOST
  config.env.cdds_token = process.env.WEB_METHODS_TOKEN

  // do not forget to return the changed config object!
  return config
}
