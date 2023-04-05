package main

import (
	"github.com/gin-gonic/gin"
)

type Response struct {
	StatusCode int         `json:"status_code"`
	Data       interface{} `json:"data"`
	Desc       string      `json:"desc"`
}

func helloHandler(c *gin.Context) {
	c.JSON(200, Response{
		StatusCode: 200,
		Data:       struct{ Msg string }{Msg: "hello world"},
		Desc:       "success",
	})
}

func main() {
	server := gin.Default()
	server.GET("/hello", helloHandler)
	server.Run("127.0.0.1:8080")
}
