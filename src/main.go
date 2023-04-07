package main

import (
	"fmt"

	"github.com/gin-gonic/gin"
)

type Response struct {
	StatusCode int         `json:"status_code"`
	Data       interface{} `json:"data"`
	Desc       string      `json:"desc"`
}

type AuthData struct {
	UserName string `json:"user_name"`
	Password string `json:"password"`
}

func helloHandler(c *gin.Context) {
	c.JSON(200, Response{
		StatusCode: 200,
		Data:       struct{ Msg string }{Msg: "hello world"},
		Desc:       "success",
	})
}

func loginHandler(c *gin.Context) {
	authData := new(AuthData)
	if err := c.BindJSON(authData); err != nil {
		fmt.Println("bind json error")
		return
	}
	if authData.UserName != "luozi" || authData.Password != "123456" {
		fmt.Println("error username or password")
		c.JSON(200, Response{
			StatusCode: 401,
			Data:       nil,
			Desc:       "unauthorized",
		})
	} else {
		c.JSON(200, Response{
			StatusCode: 200,
			Data:       nil,
			Desc:       "login success",
		})
	}
}

func main() {
	server := gin.Default()
	server.GET("/hello", helloHandler)
	server.POST("/login", loginHandler)
	server.Run("127.0.0.1:8080")
}
